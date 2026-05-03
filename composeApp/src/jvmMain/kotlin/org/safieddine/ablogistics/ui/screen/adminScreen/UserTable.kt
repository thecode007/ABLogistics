package org.safieddine.ablogistics.ui.screen.adminScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.FluentTheme
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.DatabasePerson
import io.github.composefluent.icons.filled.Delete
import io.github.composefluent.icons.filled.Pen
import io.github.composefluent.icons.filled.PersonArrowRight
import io.github.composefluent.icons.filled.PersonLock
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.data.UserDTO
import ablogistics.composeapp.generated.resources.*

@Composable
fun ColumnScope.UserTable(
    isLoading: Boolean,
    users: List<UserDTO>,
    onEdit: (UserDTO) -> Unit,
    onDelete: (UserDTO) -> Unit,
    onStatusChanges:(UserDTO, Boolean) -> Unit
) {

    if (users.isEmpty()) {
        Spacer(Modifier.weight(1f))

        Icon(modifier = Modifier.size(50.dp),
            imageVector = Icons.Filled.DatabasePerson,
            tint = FluentTheme.colors.background.smoke.default,
            contentDescription = "")

        Spacer(Modifier.height(4.dp))
        Text(text = "It seems that you are the only admin!",
            style = FluentTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = FluentTheme.colors.background.smoke.default)
        Spacer(Modifier.weight(1f))

        return
    }
    LazyColumn(Modifier.weight(1f)) {
        stickyHeader {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(FluentTheme.colors.background.mica.base.copy(alpha = 0.9f))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.full_name),
                    Modifier.weight(2f),
                    style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(Res.string.username),
                    Modifier.weight(1.5f),
                    style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(Res.string.phone),
                    Modifier.weight(1.5f),
                    style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(Res.string.actions),
                    Modifier.weight(2f),
                    style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                    textAlign = TextAlign.Center
                )
            }
            HorizontalDivider(
                color = FluentTheme.colors.background.mica.base.copy(alpha = 0.3f),
                thickness = 1.dp
            )
        }
        if (isLoading) {
            items(5) {
                UserTableShimmerRow()
                HorizontalDivider(
                    color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f),
                    thickness = 0.5.dp
                )
            }
        }
        else {
            itemsIndexed(users, key = { _, user -> user.username }) { index, user ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) Color.Transparent
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        .padding(vertical = 4.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = user.fullName,
                        textAlign = TextAlign.Center, modifier = Modifier.weight(2f))
                    Text(text = user.username,
                        textAlign = TextAlign.Center, modifier =Modifier.weight(1.5f))
                    Text(text = user.phoneNumber,
                        textAlign = TextAlign.Center, modifier =Modifier.weight(1.5f))
                    Row(
                        Modifier.weight(2f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { onEdit(user) }) {
                            Icon(
                                imageVector = Icons.Filled.Pen,
                                contentDescription = "Edit",
                                tint = FluentTheme.colors.system.attention
                            )
                        }
                        IconButton(onClick = { onDelete(user) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = FluentTheme.colors.system.critical
                            )
                        }

                        if (!user.isAdmin) {
                            IconButton(onClick = {
                                onStatusChanges(user, !user.isBlocked)
                            }) {
                                Icon(
                                    imageVector = if (user.isBlocked)
                                        Icons.Filled.PersonArrowRight
                                    else
                                        Icons.Filled.PersonLock ,
                                    contentDescription = "lock/unlock",
                                    tint = if (user.isBlocked)
                                        FluentTheme.colors.system.success
                                    else
                                        FluentTheme.colors.system.caution
                                )
                            }
                        }

                    }
                }
                HorizontalDivider(
                    color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f),
                    thickness = 0.5.dp
                )
            }
        }


    }
}