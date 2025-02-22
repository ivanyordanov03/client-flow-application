package app.quote.model;

import app.client.model.Client;
import app.user.model.User;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseModel {

    @ManyToOne
    private Client client;

    @ManyToOne
    private User companyRepresentative;

    private String deliveryAddress;
}
