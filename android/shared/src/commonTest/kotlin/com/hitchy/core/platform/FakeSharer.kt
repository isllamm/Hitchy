package com.hitchy.core.platform

class FakeSharer : Sharer {
    val sharedTexts = mutableListOf<String>()

    override fun shareText(text: String) {
        sharedTexts.add(text)
    }
}
