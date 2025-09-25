
import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class NoteTest {

    private Group group;

    @BeforeEach
    public void setUp(){
        group = new Group("Group1","Group1",false,"sati2030",LocalDateTime.now());
    }

    @Test
    public void TestCreateNoteNormal(){
        assertDoesNotThrow(() -> new Note("name",group));
    }

    @Test
    public void TestNullName(){
        assertThrows(IllegalArgumentException.class, () -> new Note(null,group));
    }

    @Test
    public void TestNullGroup(){
        assertThrows(IllegalArgumentException.class, () -> new Note("name",null));
    }

    @Test
    public void TestGetName(){
        Note note = new Note("name",group);
        assertEquals("name",note.getName());
    }

    @Test
    public void TestChangeName(){
        Note note = new Note("name",group);
        note.setName("other");
        assertEquals("other",note.getName());
    }

    @Test
    public void TestGetGroup(){
        Note note = new Note("name",group);
        assertEquals(group,note.getGroup());
    }

    public void TestGetIDNullUponCreation(){
        Note note = new Note("name",group);
        assertNull(note.getID());
    }


    @Test
    public void TestSetID(){
        Note note = new Note("name",group);
        note.setID(1);
        assertEquals(1,note.getID());
    }

    @Test
    public void TestNullChaptersUponCreation(){
        Note note = new Note("name",group);
        assertNull(note.getChapters());
    }

    @Test
    public void TestAddChapter(){
        Note note = new Note("name",group);
        Chapter chapter = new Chapter("name",note);
        note.addChapter(chapter);
        assertEquals(1, note.getChapters());
        assertSame(chapter, note.getChapters().getFirst());
    }

    @Test
    public void TestChaptersUnmodifiableList(){
        Note note = new Note("name",group);
        Chapter chapter = new Chapter("name",note);
        note.addChapter(chapter);
        assertThrows(UnsupportedOperationException.class, () -> note.getChapters().add(new Chapter("name",note)));
    }






}
