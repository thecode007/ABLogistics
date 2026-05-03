package org.safieddine.ablogistics.data

import java.awt.*
import java.awt.TrayIcon.MessageType
import java.awt.image.BufferedImage


object TrayNotifier {
    private var trayIcon: TrayIcon? = null
    private var onClick: (() -> Unit)? = null

    fun init() {
        if (!SystemTray.isSupported()) return
        if (trayIcon != null) return

        val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        g.color = Color(0, 122, 204)
        g.fillRect(0, 0, 16, 16)
        g.dispose()

        val icon = TrayIcon(image, "Warehouse Hub")
        icon.isImageAutoSize = true
        icon.toolTip = "Warehouse Hub"

        try {
            SystemTray.getSystemTray().add(icon)
            trayIcon = icon
            icon.addActionListener {
                try { onClick?.invoke() } catch (_: Exception) {}
            }
            icon.addMouseListener(object: java.awt.event.MouseAdapter() {
                override fun mouseClicked(e: java.awt.event.MouseEvent?) {
                    try { onClick?.invoke() } catch (_: Exception) {}
                }
            })
        } catch (_: Exception) { /* ignore */ }
    }

    fun show(title: String, body: String) {
        val lowerBody = body.lowercase()
        if (!lowerBody.contains("new load created") && !lowerBody.contains("shortage finalized")) {
            return
        }

        val icon = trayIcon ?: return
        try {
            icon.displayMessage(title, body, MessageType.INFO)
        } catch (_: Exception) { /* ignore */ }
    }

    fun setOnClick(handler: (() -> Unit)?) {
        onClick = handler
    }
}
