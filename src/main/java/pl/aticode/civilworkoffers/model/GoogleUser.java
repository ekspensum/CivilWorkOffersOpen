package pl.aticode.civilworkoffers.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GoogleUser {

    private String sub;
    private String name;
    private String given_name;
    private String family_name;
    private String email;
    private String picture;
    private String locale;
}
