package ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpModel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

public class AmqpPayload {
    private String name;
    private Long id;
    private String desc;
    private Instant creation;

    public AmqpPayload() {
        //default constructor to comply with POJO
    }

    public AmqpPayload(String name, Long id, String desc, Instant creation) {
        this.name = name;
        this.id = id;
        this.desc = desc;
        this.creation = creation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getCreation() {
        return creation.getEpochSecond();
    }

    public void setCreation(Long creation) {
        this.creation = Instant.ofEpochSecond(creation);
    }

    @JsonIgnore
    public Instant getCreationTime(){
        return creation;
    }
}
