package ddns.net.vigmini.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import ddns.net.vigmini.R
import java.lang.ClassCastException

class ChangePasswordDialog: DialogFragment() {
    internal lateinit var listener: ChangePasswordDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_change_password, null)
        val oldPwEditText: EditText = view.findViewById(R.id.dialogCP_oldPwEditText)
        val newPwEditText: EditText = view.findViewById(R.id.dialogCP_newPwEditText)
        val newWdhPwEditText: EditText = view.findViewById(R.id.dialogCP_newPwWdhEditText)

        builder.setView(view)
            .setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, i: Int ->

            }
            .setPositiveButton(R.string.ok){ dialogInterface: DialogInterface, i: Int ->
                val oldPw: String = oldPwEditText.text.toString()
                val newPw: String = newPwEditText.text.toString()
                val newPwWdh: String = newWdhPwEditText.text.toString()
                listener.applyPassword(oldPw, newPw, newPwWdh)
            }

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            listener = context as ChangePasswordDialogListener
        }catch (e: ClassCastException){
            throw ClassCastException(context.toString() +
                    "Must implement ChangePasswordDialogListener")
        }
    }

    interface ChangePasswordDialogListener{
        fun applyPassword(oldPw: String, newPw: String, newPwWdh: String)
    }

}
