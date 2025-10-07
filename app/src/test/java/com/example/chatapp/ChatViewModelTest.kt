package com.example.chatapp

import app.cash.turbine.test
import com.example.chatapp.chatScreen.ChatEffect
import com.example.chatapp.chatScreen.ChatIntent
import com.example.chatapp.chatScreen.ChatViewModel
import com.example.chatapp.core.datastore.DataStoreManager
import com.example.domain.entity.Message
import com.example.domain.entity.MessageStatus
import com.example.domain.entity.User
import com.example.domain.usecase.GetUserUseCase
import com.example.domain.usecase.ObserveMessagesUseCase
import com.example.domain.usecase.RetryMessageUseCase
import com.example.domain.usecase.SendMessageUseCase
import com.example.domain.utils.ResultWrapper
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)

class ChatViewModelTest {

    private lateinit var viewModel: ChatViewModel
    private lateinit var observeMessagesUseCase: ObserveMessagesUseCase
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var retryMessageUseCase: RetryMessageUseCase
    private lateinit var getUserUseCase: GetUserUseCase
    private lateinit var dataStoreManager: DataStoreManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        observeMessagesUseCase = mockk()
        sendMessageUseCase = mockk()
        retryMessageUseCase = mockk()
        getUserUseCase = mockk()
        dataStoreManager = mockk()

        coEvery { dataStoreManager.getUserId() } returns flowOf("123" )

        coEvery { dataStoreManager.getUsername() } returns flowOf("TestUser" )

        coEvery { dataStoreManager.getImage() } returns flowOf("")

        coEvery { getUserUseCase(any()) } returns ResultWrapper.Success(
            User(
                id = "123",
                username = "TestUser",
                profileImage = ""
            )
        )

        every { observeMessagesUseCase() } returns flowOf(emptyList())

        viewModel = ChatViewModel(
            observeMessagesUseCase,
            sendMessageUseCase,
            retryMessageUseCase,
            getUserUseCase,
            dataStoreManager,
            mockk(relaxed = true)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage should add message with SENDING status`() = runTest {
        advanceUntilIdle()

        viewModel.process(ChatIntent.SendText("Hello"))
        val messages = viewModel.state.value.messages

        assertTrue(messages.isNotEmpty())
        assertEquals("Hello", messages.last().content)
        assertEquals(MessageStatus.SENDING, messages.last().status)
    }

    @Test
    fun `retryMessage should change status to SENDING`() = runTest {

        advanceUntilIdle()

        val message = Message(
            id = "1", userId = "123", username = "TestUser",
            profileImage = "", content = "test",
            createdAt = 0, status = MessageStatus.FAILED
        )

        viewModel.process(ChatIntent.UpdateComposingText("test"))

        viewModel.retryMessage(message.id)

        viewModel.state.value.copy(messages = listOf(message))

        val updated = message.copy(status = MessageStatus.SENDING)
        assertEquals(MessageStatus.SENDING, updated.status)
    }

    @Test
    fun `update composing text`() = runTest {
        viewModel.process(ChatIntent.UpdateComposingText("Typing..."))
        assertEquals("Typing...", viewModel.state.value.composingText)
    }

    @Test
    fun `should send effect when empty message`() = runTest {

        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.process(ChatIntent.SendText(""))

            val effect = awaitItem()

            assertTrue(effect is ChatEffect.ShowToast)
        }

    }



}
