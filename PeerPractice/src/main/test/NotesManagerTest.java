
import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class NotesManagerTest {

    NotesManager notesManager;
    User user;
    Group group;
    INotesDAO notesDAO = new MockNotesDao();
    IGroupDAO groupDAO = new MockGroupDAO();

    @BeforeEach
    public void setUp(){
        notesManager = new NotesManager(notesDAO,groupDAO);
    }




}
