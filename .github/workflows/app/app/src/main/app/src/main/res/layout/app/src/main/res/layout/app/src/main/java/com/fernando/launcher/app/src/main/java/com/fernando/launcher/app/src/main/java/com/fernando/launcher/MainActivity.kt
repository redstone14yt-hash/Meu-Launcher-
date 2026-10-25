package com.fernando.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: View
    private lateinit var search: EditText
    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawer = findViewById(R.id.drawer)
        search = findViewById(R.id.search)

        adapter = AppAdapter { launch(it) }
        val list = findViewById<RecyclerView>(R.id.apps)
        list.layoutManager = GridLayoutManager(this, 4)
        list.adapter = adapter

        findViewById<Button>(R.id.openDrawer).setOnClickListener { openDrawer() }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        search.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                adapter.firstOrNull()?.let { launch(it) }
                true
            } else false
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawer.visibility == View.VISIBLE) closeDrawer()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadApps()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (drawer.visibility == View.VISIBLE) closeDrawer()
    }

    private fun loadApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .map {
                AppInfo(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    activityName = it.activityInfo.name,
                    icon = it.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase() }
        adapter.submit(apps)
    }

    private fun launch(app: AppInfo) {
        val i = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        startActivity(i)
        closeDrawer()
    }

    private fun openDrawer() {
        drawer.visibility = View.VISIBLE
        search.requestFocus()
    }

    private fun closeDrawer() {
        search.setText("")
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(search.windowToken, 0)
        drawer.visibility = View.GONE
    }
}
