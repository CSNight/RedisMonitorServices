package csnight.redis.monitor.rest.rms.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class InsRightsDto {
    @NotEmpty
    @NotNull
    private String username;
    @NotEmpty
    @NotNull
    private String ins_id;
    @NotNull
    private String commands;


    public String getCommands() {
        return commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIns_id() {
        return ins_id;
    }

    public void setIns_id(String ins_id) {
        this.ins_id = ins_id;
    }
}
