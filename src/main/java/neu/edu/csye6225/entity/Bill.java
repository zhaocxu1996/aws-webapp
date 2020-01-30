package neu.edu.csye6225.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * @author zhaocxu
 */
@Entity
@Data
public class Bill {
    /**
     * Payment Type
     */
    public enum PaymentStatus {
        paid,
        due,
        past_due,
        no_payment_required;
    }

    /**
     * example: d290f1ee-6c54-4b01-90e6-d701748f0851
     * readOnly: true
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid")
    private String id;
    /**
     * example: 2016-08-29T09:12:33.001Z
     * readOnly: true
     */
    private String created_ts;
    /**
     * example: 2016-08-29T09:12:33.001Z
     * readOnly: true
     */
    private String updated_ts;
    /**
     * example: a460a1ef-6d54-4b01-90e6-d7017sad851
     * readOnly: true
     */
    private String owner_id;
    /**
     * example: Northeastern University
     */
    private String vendor;
    /**
     * example: 2020-01-06
     */
    private String bill_date;
    /**
     * example: 2020-01-12
     */
    private String due_date;
    /**
     * example: 7000.51
     * minimum: 0.01
     */
    private double amount_due;
    /**
     * uniqueItems: true
     * example: List [ "college", "tuition", "spring2020" ]
     */
    @ElementCollection
    private List<String> categories;
    /**
     * Enum:
     * [ paid, due, past_due, no_payment_required ]
     */
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}
