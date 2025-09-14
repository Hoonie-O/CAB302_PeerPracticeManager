import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GroupManagerTest {

    private GroupManager groupManager;
    private IGroupDAO groupDAO;
    private Notifier notifier;
    private UserDAO userDAO;

    @BeforeEach
    public void setUp(){
        groupDAO = new MockGroupDAO();

        groupManager = new GroupManager()
    }

}
