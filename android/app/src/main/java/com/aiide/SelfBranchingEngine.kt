package com.aiide

import android.content.Context
import org.json.JSONObject

class SelfBranchingEngine(private val context: Context) {

    private val branches = mutableListOf<Branch>()

    data class Branch(val name: String, val status: String)

    fun createBranch(name: String): Boolean {
        branches.add(Branch(name, "active"))
        return true
    }

    fun listBranches(): List<Branch> = branches.toList()

    fun switchBranch(name: String): Boolean {
        return branches.any { it.name == name }
    }
}
