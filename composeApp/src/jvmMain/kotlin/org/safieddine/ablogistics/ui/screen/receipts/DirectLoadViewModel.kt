package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.runtime.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.service.*
import java.math.BigDecimal
import java.math.RoundingMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.safieddine.ablogistics.data.session.GlobalPriceStore
import org.safieddine.ablogistics.data.session.SessionStore
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class DirectLoadViewModel : ViewModel() {
    private val _customers = MutableStateFlow<List<CustomerResponse>>(emptyList())
    val customers: StateFlow<List<CustomerResponse>> = _customers


    private val _brvs = MutableStateFlow<List<BRVDTO>>(emptyList())
    val brvs: StateFlow<List<BRVDTO>> = _brvs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success

    // Form State
    var selectedCustomer by mutableStateOf<CustomerResponse?>(null)
    var selectedBrv by mutableStateOf<BRVDTO?>(null)
    var selectedMaterial by mutableStateOf(MaterialType.FUEL)
    var loadedQuantity by mutableStateOf("")
    var costPrice by mutableStateOf("")
    var sellingPrice by mutableStateOf("")
    var brvCost by mutableStateOf("")
    var description by mutableStateOf("")
    var receiptId by mutableStateOf("")

    // Search and Payment States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    var supplierPaid by mutableStateOf(false)
    var customerPaid by mutableStateOf(false)
    
    var isEditMode by mutableStateOf(false)
    var editingReceiptId by mutableStateOf<Long?>(null)
    var originalReceiptId by mutableStateOf("")
    var isPaymentOnlyEdit by mutableStateOf(false)
    
    var customerFilter by mutableStateOf<CustomerResponse?>(null)

    fun prepareCreate() {
        isEditMode = false
        isPaymentOnlyEdit = false
        editingReceiptId = null
        receiptId = ""
        originalReceiptId = ""
        selectedCustomer = null
        selectedBrv = null
        loadedQuantity = ""
        brvCost = ""
        description = ""
        supplierPaid = false
        customerPaid = false
        _error.value = null
        _success.value = null
        autoFillPrices(selectedMaterial, GlobalPriceStore.prices.value)
    }

    fun onCustomerFilterChanged(customer: CustomerResponse?, warehouseId: Long) {
        customerFilter = customer
        fetchLoads(warehouseId)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    init {
        // Sync prices when they load from the store
        viewModelScope.launch {
            GlobalPriceStore.prices.collect { prices ->
                if (!isEditMode) autoFillPrices(selectedMaterial, prices)
            }
        }
        
        // Debounced search
        viewModelScope.launch {
            searchQuery.debounce(350).collect {
                val whId = SessionStore.selectedWarehouse.value?.id
                if (whId != null) fetchLoads(whId)
            }
        }
    }

    fun onMaterialChanged(type: MaterialType) {
        selectedMaterial = type
        autoFillPrices(type, GlobalPriceStore.prices.value)
    }

    private fun autoFillPrices(type: MaterialType, prices: List<MaterialPriceDTO>) {
        if (prices.isEmpty()) return
        val price = prices.find { it.materialType == type }
        if (price != null) {
            costPrice = price.costPrice.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
            sellingPrice = price.sellingPrice.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
            updateCalculations()
        }
    }

    // Preview state
    var projectedRevenue by mutableStateOf(BigDecimal.ZERO)
    var projectedProfit by mutableStateOf(BigDecimal.ZERO)

    private val _loads = MutableStateFlow<List<ReceiptResponse>>(emptyList())
    val loads: StateFlow<List<ReceiptResponse>> = _loads

    private val _isFinalizing = MutableStateFlow(false)
    val isFinalizing: StateFlow<Boolean> = _isFinalizing

    fun loadInitialData(warehouseId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val custRes = CustomerService.list(warehouseId)
                _customers.value = custRes.getOrNull()?.data?.customers?.filter { it.warehouseId == warehouseId } ?: emptyList()


                val fleetRes = BRVService.getFleetStatus()
                _brvs.value = fleetRes.getOrNull()?.data?.brvs ?: emptyList()

                // Ensure prices are loaded
                if (GlobalPriceStore.prices.value.isEmpty()) {
                    val priceRes = PriceService.getGlobalPrices()
                    if (priceRes.isSuccess) {
                        val newPrices = priceRes.getOrNull()?.data ?: emptyList()
                        GlobalPriceStore.updatePrices(newPrices)
                        autoFillPrices(selectedMaterial, newPrices)
                    }
                } else {
                    autoFillPrices(selectedMaterial, GlobalPriceStore.prices.value)
                }

                fetchLoads(warehouseId)
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchLoads(warehouseId: Long) {
        viewModelScope.launch {
            val res = ReceiptService.listWarehouseDetailed(
                warehouseId = warehouseId,
                customerId = customerFilter?.id,
                type = ReceiptType.OUTWARD,
                size = 100,
                receiptId = _searchQuery.value.ifBlank { null }
            )
            if (res.isSuccess) {
                // Filter for receipts that are associated with a BRV (Load Orders)
                _loads.value = res.getOrNull()?.data?.receipts?.filter { it.brvId != null } ?: emptyList()
            }
        }
    }

    fun updateCalculations() {
        val qty = loadedQuantity.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val cp = costPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val sp = sellingPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val bc = brvCost.toBigDecimalOrNull() ?: BigDecimal.ZERO

        projectedRevenue = sp.multiply(qty).add(bc).setScale(4, RoundingMode.HALF_UP)
        projectedProfit = sp.subtract(cp).multiply(qty).setScale(4, RoundingMode.HALF_UP)
    }

    fun processLoad(warehouseId: Long) {
        if (receiptId.isBlank()) {
            _error.value = "Receipt ID is mandatory"
            return
        }
        val qty = loadedQuantity.toBigDecimalOrNull() ?: return
        val cp = costPrice.toBigDecimalOrNull() ?: return
        val sp = sellingPrice.toBigDecimalOrNull() ?: return
        val bc = brvCost.toBigDecimalOrNull() ?: return
        val cust = selectedCustomer ?: return
        val brv = selectedBrv ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null
            
            val req = ProcessLoadRequest(
                brvId = brv.id,
                customerId = cust.id,
                warehouseId = warehouseId,
                materialType = selectedMaterial,
                material = selectedMaterial.name,
                loadedQuantity = qty,
                costPrice = cp,
                sellingPrice = sp,
                brvCost = bc,
                description = description.ifBlank { "${selectedMaterial.name} Load: ${brv.plateNumber}" },
                receiptId = receiptId.trim()
            )

            val res = BRVService.processLoad(req)
            if (res.isSuccess) {
                handlePaymentReceipts(warehouseId, receiptId.trim(), cust.id, qty, cp)
                _success.value = "Load processed successfully! Linked receipts created."
                clearForm()
                fetchLoads(warehouseId)
            } else {
                _error.value = res.exceptionOrNull()?.message ?: "Unknown error"
                println("ProcessLoad Error: ${_error.value}")
            }
            _isLoading.value = false
        }
    }

    private suspend fun handlePaymentReceipts(
        warehouseId: Long,
        rid: String,
        customerId: Long,
        qty: BigDecimal,
        cp: BigDecimal
    ) {
        if (rid.isBlank()) return
        val payRid = "Pay-$rid"
        
        if (supplierPaid) {
            val supReq = CreateReceiptRequest(
                receiptId = payRid,
                receiptType = ReceiptType.OUTWARD,
                entityType = EntityType.WAREHOUSE,
                warehouseId = warehouseId,
                amount = cp.multiply(qty),
                description = "Payment to supplier for load $rid"
            )
            ReceiptService.create(supReq)
        }
        
        if (customerPaid) {
            val custReq = CreateReceiptRequest(
                receiptId = payRid,
                receiptType = ReceiptType.INWARD,
                entityType = EntityType.CUSTOMER,
                warehouseId = warehouseId,
                customerId = customerId,
                amount = projectedRevenue,
                description = "Payment from customer for load $rid"
            )
            ReceiptService.create(custReq)
        }
    }

    fun prepareEdit(load: ReceiptResponse) {
        isEditMode = true
        isPaymentOnlyEdit = load.dispatchedQuantity != null
        editingReceiptId = load.id
        selectedCustomer = customers.value.find { it.id == load.customerId }
        selectedBrv = brvs.value.find { it.id == load.brvId }
        selectedMaterial = load.materialType ?: MaterialType.FUEL
        loadedQuantity = load.loadedQuantity?.toPlainString() ?: ""
        costPrice = load.costPrice?.toPlainString() ?: ""
        sellingPrice = load.sellingPrice?.toPlainString() ?: ""
        brvCost = load.brvCost?.toPlainString() ?: ""
        description = load.description ?: ""
        receiptId = load.receiptId ?: ""
        originalReceiptId = receiptId
        updateCalculations()
        
        // Check if payment receipts already exist
        viewModelScope.launch {
            val payRid = "Pay-$receiptId"
            val res = ReceiptService.listWarehouseDetailed(load.warehouseId, receiptId = payRid)
            if (res.isSuccess) {
                val receipts = res.getOrNull()?.data?.receipts ?: emptyList()
                // For supplier payment, check for WAREHOUSE type with OUTWARD and exact Pay-rid
                supplierPaid = receipts.any { it.entityType == EntityType.WAREHOUSE && it.receiptType == ReceiptType.OUTWARD && it.receiptId == payRid }
                customerPaid = receipts.any { it.entityType == EntityType.CUSTOMER && it.receiptId == payRid }
            }
        }
    }

    fun updateLoad(warehouseId: Long) {
        if (receiptId.isBlank()) {
            _error.value = "Receipt ID is mandatory"
            return
        }
        val loadId = editingReceiptId ?: return
        val rid = receiptId.trim()
        val qty = loadedQuantity.toBigDecimalOrNull() ?: return
        val cp = costPrice.toBigDecimalOrNull() ?: return
        val sp = sellingPrice.toBigDecimalOrNull() ?: return
        val bc = brvCost.toBigDecimalOrNull() ?: return
        val custId = selectedCustomer?.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val req = UpdateReceiptRequest(
                receiptId = rid,
                receiptType = ReceiptType.OUTWARD,
                entityType = EntityType.CUSTOMER,
                amount = sp.multiply(qty).add(bc),
                description = description.ifBlank { null },
                customerId = custId
            )
            
            val res = ReceiptService.update(loadId, req)
            if (res.isSuccess) {
                // Update payment receipts
                handleUpdatePaymentReceipts(warehouseId, rid, custId, qty, cp)
                _success.value = "Load updated successfully."
                clearForm()
                fetchLoads(warehouseId)
            } else {
                _error.value = res.exceptionOrNull()?.message ?: "Update failed"
            }
            _isLoading.value = false
        }
    }

    private suspend fun handleUpdatePaymentReceipts(
        warehouseId: Long,
        rid: String,
        customerId: Long,
        qty: BigDecimal,
        cp: BigDecimal
    ) {
        if (rid.isBlank()) return
        
        // If rid changed, clean up old convention receipts first
        if (originalReceiptId.isNotBlank() && originalReceiptId != rid) {
            val oldPayRid = "Pay-$originalReceiptId"
            val oldRes = ReceiptService.listWarehouseDetailed(warehouseId, receiptId = oldPayRid)
            oldRes.getOrNull()?.data?.receipts?.forEach { 
                if (it.receiptId == oldPayRid) ReceiptService.delete(it.id)
            }
        }

        val payRid = "Pay-$rid"
        
        // Fetch current payment receipts
        val existingRes = ReceiptService.listWarehouseDetailed(warehouseId, receiptId = payRid)
        val existing = existingRes.getOrNull()?.data?.receipts ?: emptyList()
        
        // Filter to ensure we only touch receipts matching exactly Pay-rid
        // Supplier payment is now WAREHOUSE entity type
        val supReceipt = existing.find { it.entityType == EntityType.WAREHOUSE && it.receiptType == ReceiptType.OUTWARD && it.receiptId == payRid }
        val custReceipt = existing.find { it.entityType == EntityType.CUSTOMER && it.receiptId == payRid }
        
        // Supplier Payment
        if (supplierPaid && supReceipt == null) {
            // Create
            ReceiptService.create(CreateReceiptRequest(payRid, ReceiptType.OUTWARD, EntityType.WAREHOUSE, warehouseId, null, cp.multiply(qty), "Payment to supplier for load $rid"))
        } else if (!supplierPaid && supReceipt != null) {
            // Delete
            ReceiptService.delete(supReceipt.id)
        } else if (supplierPaid && supReceipt != null) {
            // Update amount if changed
            val newAmount = cp.multiply(qty)
            if (supReceipt.amount != newAmount) {
                ReceiptService.update(supReceipt.id, UpdateReceiptRequest(payRid, ReceiptType.OUTWARD, EntityType.WAREHOUSE, newAmount, supReceipt.description))
            }
        }
        
        // Customer Payment
        if (customerPaid && custReceipt == null) {
            // Create
            ReceiptService.create(CreateReceiptRequest(payRid, ReceiptType.INWARD, EntityType.CUSTOMER, warehouseId, customerId, projectedRevenue, "Payment from customer for load $rid"))
        } else if (!customerPaid && custReceipt != null) {
            // Delete
            ReceiptService.delete(custReceipt.id)
        } else if (customerPaid && custReceipt != null) {
            // Update
            if (custReceipt.amount != projectedRevenue) {
                ReceiptService.update(custReceipt.id, UpdateReceiptRequest(payRid, ReceiptType.INWARD, EntityType.CUSTOMER, projectedRevenue, custReceipt.description, customerId))
            }
        }
    }

    fun finalizeLoad(warehouseId: Long, customerReceiptId: Long, dispatchedQty: BigDecimal) {
        viewModelScope.launch {
            _isFinalizing.value = true
            val req = FinalizeDeliveryRequest(customerReceiptId, dispatchedQty)
            val res = BRVService.finalizeDelivery(req)
            if (res.isSuccess) {
                _success.value = "Delivery finalized successfully."
                fetchLoads(warehouseId)
            } else {
                _error.value = res.exceptionOrNull()?.message ?: "Failed to finalize"
            }
            _isFinalizing.value = false
        }
    }

    fun reverseFinalization(warehouseId: Long, receiptId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = ReceiptService.reverseFinalization(receiptId)
            _isLoading.value = false
            if (res.isSuccess) {
                _success.value = "Finalization reversed"
                fetchLoads(warehouseId)
            } else {
                _error.value = res.exceptionOrNull()?.message
            }
        }
    }

    fun deleteLoad(warehouseId: Long, receiptId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = ReceiptService.deleteLoad(receiptId)
            _isLoading.value = false
            if (res.isSuccess) {
                _success.value = "Load deleted successfully"
                fetchLoads(warehouseId)
            } else {
                _error.value = res.exceptionOrNull()?.message
            }
        }
    }

    private fun clearForm() {
        isEditMode = false
        isPaymentOnlyEdit = false
        editingReceiptId = null
        selectedCustomer = null
        selectedBrv = null
        loadedQuantity = ""
        costPrice = ""
        sellingPrice = ""
        brvCost = ""
        description = ""
        receiptId = ""
        projectedRevenue = BigDecimal.ZERO
        projectedProfit = BigDecimal.ZERO
        supplierPaid = false
        customerPaid = false
    }
}

fun String.toBigDecimalOrNull(): BigDecimal? {
    return try {
        BigDecimal(this.replace(",", ""))
    } catch (e: Exception) {
        null
    }
}
