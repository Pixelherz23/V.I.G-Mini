package ddns.net.vigmini.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import ddns.net.vigmini.R
import java.lang.ClassCastException

class RegisterGreenhouseDialog: DialogFragment() {
    internal lateinit var listener: RegisterGreenhouseDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_register_greenhouse, null)
        val productKeyEditText: EditText = view.findViewById(R.id.dialogRG_productKeyEditText)

        builder.setView(view)
            .setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, i: Int ->

            }
            .setPositiveButton(R.string.ok){ dialogInterface: DialogInterface, i: Int ->
                val productKey: String = productKeyEditText.text.toString()
                listener.applyText(productKey)
            }

        return builder.create()
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        try {
            listener = context as RegisterGreenhouseDialogListener
        }catch (e: ClassCastException){
            throw ClassCastException(context.toString() +
                    "must implement RegisterGreenhouseDialogListener")
        }


    }

    interface RegisterGreenhouseDialogListener{
        fun applyText(productKey: String)
    }
}



