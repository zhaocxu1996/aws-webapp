package neu.edu.csye6225.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class MetaData {

    @Id
    @Column(length = 125)
    private String file_id;
    private String md5;
    private long size;
    private String type;

}
