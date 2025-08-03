import com.example.kokoro82m.utils.AudioPlayer
import com.example.kokoro82m.utils.PlayerState
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
class AudioPlayerSimpleTest {
    private lateinit var player: AudioPlayer
    private lateinit var audioTrack: AudioTrack

    @Before
    fun setup() {
        audioTrack = mock(AudioTrack::class.java)
        player = AudioPlayer(CoroutineScope(Dispatchers.Unconfined)) {}
        val field = AudioPlayer::class.java.getDeclaredField("audioTrack")
        field.isAccessible = true
        field.set(player, audioTrack)
        val logField = Class.forName("com.example.kokoro82m.utils.DebugLogger").getDeclaredField("logFile")
        logField.isAccessible = true
        logField.set(null, java.io.File.createTempFile("log", ".txt"))
        val pcmField = AudioPlayer::class.java.getDeclaredField("pcmData")
        pcmField.isAccessible = true
        pcmField.set(player, ByteArray(100))
    }

    @Test
    fun playStartsPlayback() = runBlocking {
        player.play()
        delay(50)
        assertEquals(PlayerState.PLAYING, player.getState())
        player.stop()
    }

    @Test
    fun stopReleasesResources() {
        val stateField = AudioPlayer::class.java.getDeclaredField("currentState")
        stateField.isAccessible = true
        stateField.set(player, PlayerState.PLAYING)

        player.stop()

        assertEquals(PlayerState.IDLE, player.getState())
        assertEquals(0, player.getPosition())
    }
}
