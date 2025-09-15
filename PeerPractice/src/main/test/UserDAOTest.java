import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

public class UserDAOTest {
    private IUserDAO userDAO;
    private PasswordHasher passHasher;
    private UserManager userManager;
    private User user;

    private String firstName = "Holly";
    private String lastName = "Spain";
    private String username = "Hollyfloweer";
    private String email = "n11618230@qut.edu.au";
    private String password = "Hollypassword123*";
    private String institution = "QUT";

    @BeforeEach
    public void setUp() {
        try {
            userDAO = new UserDAO();
            passHasher = new BcryptHasher();
            userManager = new UserManager(userDAO, passHasher);
            user = new User(firstName, lastName, username, email, password, institution);
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    @Test
    public void change_password() {
        try {

        } catch {

        }
    }
}
