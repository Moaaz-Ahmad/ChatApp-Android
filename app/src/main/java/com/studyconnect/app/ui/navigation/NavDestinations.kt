package com.studyconnect.app.ui.navigation

object NavDestinations {
    const val Splash = "splash"
    const val AuthGraph = "auth"
    const val Login = "auth/login"
    const val SignUp = "auth/signup"
    const val ChatList = "chat/list"
    const val ChatRoom = "chat/room"
    const val CreateRoom = "chat/create"
    const val Profile = "profile"
    const val ChatRoomIdArg = "roomId"

    fun chatRoomRoute(roomId: String) = "$ChatRoom/$roomId"
    val chatRoomPattern = "$ChatRoom/{$ChatRoomIdArg}"
}
