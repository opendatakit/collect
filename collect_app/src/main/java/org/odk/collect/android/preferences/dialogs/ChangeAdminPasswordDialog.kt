package org.odk.collect.android.preferences.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import org.odk.collect.android.R
import org.odk.collect.android.databinding.PasswordDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.utilities.SoftKeyboardController
import org.odk.collect.android.utilities.ToastUtils.showShortToast
import org.odk.collect.androidshared.livedata.SingleEventLiveData
import javax.inject.Inject

class ChangeAdminPasswordDialog : DialogFragment() {
    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var softKeyboardController: SoftKeyboardController

    lateinit var binding: PasswordDialogLayoutBinding

    val model: ChangeAdminPasswordViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = PasswordDialogLayoutBinding.inflate(LayoutInflater.from(context))

        binding.pwdField.post {
            softKeyboardController.showSoftKeyboard(binding.pwdField)
        }
        binding.checkBox2.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                binding.pwdField.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                binding.pwdField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.change_admin_password)
            .setView(binding.root)
            .setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, _: Int ->
                val password = binding.pwdField.text.toString()

                settingsProvider.getAdminSettings().save(AdminKeys.KEY_ADMIN_PW, password)

                if (password.isEmpty()) {
                    showShortToast(R.string.admin_password_disabled)
                    model.passwordSet(false)
                } else {
                    showShortToast(R.string.admin_password_changed)
                    model.passwordSet(true)
                }
                dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { _: DialogInterface?, _: Int -> dismiss() }
            .setCancelable(false)
            .create()
    }
}

class ChangeAdminPasswordViewModel : ViewModel() {
    val passwordSet = SingleEventLiveData<Boolean>()

    fun passwordSet(isPasswordSet: Boolean) {
        passwordSet.value = isPasswordSet
    }
}
