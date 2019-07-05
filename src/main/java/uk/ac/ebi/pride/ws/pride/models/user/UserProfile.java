package uk.ac.ebi.pride.ws.pride.models.user;

import uk.ac.ebi.pride.archive.dataprovider.utils.TitleConstants;

public class UserProfile {

    private String email;
    private TitleConstants title;
    private String firstName;
    private String lastName;
    private String affiliation;
    private String country;
    private String orcid;
    private Boolean acceptedTermsOfUse;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TitleConstants getTitle() {
        return title;
    }

    public void setTitle(TitleConstants title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    public Boolean getAcceptedTermsOfUse() {
        return acceptedTermsOfUse;
    }

    public void setAcceptedTermsOfUse(Boolean acceptedTermsOfUse) {
        this.acceptedTermsOfUse = acceptedTermsOfUse;
    }
}
