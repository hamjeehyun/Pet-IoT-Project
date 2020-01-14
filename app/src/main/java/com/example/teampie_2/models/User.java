package com.example.teampie_2.models;

public class User {

    public String username;
    public String useremail;
    public String petImageUri;
    public String petname;
    public String petage;
    public String petweight;

    public User() {
        petImageUri = "https://firebasestorage.googleapis.com/v0/b/teampie-0316.appspot.com/o/Upload-Profile%2F%EA%B7%B8%EB%A6%BC2.png?alt=media&token=faa7a6e4-c217-4cd2-975d-34c4fc871048" +
                "https://firebasestorage.googleapis.com/v0/b/teampie-0316.appspot.com/o/Upload-Profile%2F%EA%B7%B8%EB%A6%BC2.png?alt=media&token=faa7a6e4-c217-4cd2-975d-34c4fc871048";
    }

    public User(String username, String useremail, String petImageUri, String petname, String petage, String petweight) {
        this.username = username;
        this.useremail = useremail;
        this.petImageUri = petImageUri;
        this.petname = petname;
        this.petage = petage;
        this.petweight = petweight;
    }

    public String getUseremail() {
        return useremail;
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }

    public String getPetImageUri() {
        return petImageUri;
    }

    public void setPetImageUri(String petImageUri) {
        this.petImageUri = petImageUri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return useremail;
    }

    public void setEmail(String useremail) {
        this.useremail = useremail;
    }

    public String getPetname() {
        return petname;
    }

    public void setPetname(String petname) {
        this.petname = petname;
    }

    public String getPetage() {
        return petage;
    }

    public void setPetage(String petage) {
        this.petage = petage;
    }

    public String getPetweight() {
        return petweight;
    }

    public void setPetweight(String petweight) {
        this.petweight = petweight;
    }
}
