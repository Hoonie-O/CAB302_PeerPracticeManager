import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class GroupTest {

    private Group group;
    private MockUserDAO mockUserDAO;
    private User user;
    private String USERNAME = "sati2030";

    @BeforeEach
    public void setUp(){
        mockUserDAO = new MockUserDAO();
        user = new User("Seiji","Sato",USERNAME,"email@email.com","asfasfasfasf","QUT");
        mockUserDAO.addUser(user);
        group = new Group("Group1","Group for CAB302",false,USERNAME, LocalDateTime.now());
    }

    @Test
    public void test






}
