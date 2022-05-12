package com.sharkaboi.yogapartner.modules.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.sharkaboi.yogapartner.R

class ToolBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {

    private var iconView: ImageView? = null
    private var listener: (() -> Unit)? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        iconView = holder.findViewById(android.R.id.icon) as ImageView
        iconView?.let {
            it.setPadding(48, 0, 0, 0)
            it.setOnClickListener {
                listener?.invoke()
            }
        }
        holder.itemView.setBackgroundColor(
            ResourcesCompat.getColor(
                context.resources,
                R.color.dark_black,
                context.theme
            )
        )
    }

    override fun onDetached() {
        iconView = null
        listener = null
        super.onDetached()
    }

    fun setNavigationIconListener(action: () -> Unit) {
        listener = action
        iconView?.let {
            it.setOnClickListener {
                action()
            }
        }
    }
}
