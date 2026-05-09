package com.aiide

// Compatibility shim for explicit lambda parameters that still reference `it`.
val it: String
    get() = ""
