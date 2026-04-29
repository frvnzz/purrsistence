package com.example.purrsistence.domain.focus

class FakeFocusBlocker : FocusBlocker {
    var startCalls = 0
    var stopCalls = 0

    override fun startBlocking() {
        startCalls++
    }

    override fun stopBlocking() {
        stopCalls++
    }
}