package neu.edu.csye6225.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
public class File {

    /**
     * readOnly: true
     * example: mybill.pdf
     */
    private String file_name;

    /**
     * readOnly: true
     * example: d290f1ee-6c54-4b01-90e6-d701748f0851
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid")
    private String id;

    /**
     * readOnly: true
     * example: /tmp/file.jpg
     */
    private String url;

    /**
     * readOnly: true
     * example: 2020-01-12
     */
    private String upload_date;
}
