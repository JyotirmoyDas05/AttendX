package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResetDataDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    resetData: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = shapes.extraLarge,
            color = colorScheme.surfaceContainerHigh,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    painter = painterResource(R.drawable.restart),
                    contentDescription = null,
                    tint = colorScheme.secondary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(24.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.reset_data_dialog_title),
                    textAlign = TextAlign.Center,
                    style = typography.headlineSmall,
                    color = colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.reset_data_dialog_text),
                    style = typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shapes = ButtonDefaults.shapes(),
                    ) { Text(stringResource(R.string.cancel)) }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = resetData,
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}
