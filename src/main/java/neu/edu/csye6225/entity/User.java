package neu.edu.csye6225.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author zhaocxu
 */
@Data
@Entity
public class User {

    /**
     * example: d290f1ee-6c54-4b01-90e6-d701748f0851
     * readOnly: true
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid")
    private String id;
    /**
     * example: Jane
     */
    private String first_name;
    /**
     * example: Doe
     */
    private String last_name;
    /**
     * example: skdjfhskdfjhg
     * writeOnly: true
     */
    private String password;
    /**
     * example: jane.doe@example.com
     */
    private String email_address;
    /**
     * example: 2016-08-29T09:12:33.001Z
     * readOnly: true
     */
    private String account_created;
    /**
     * example: 2016-08-29T09:12:33.001Z
     * readOnly: true
     */
    private String account_updated;

}
