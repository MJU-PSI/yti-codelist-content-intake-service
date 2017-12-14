package fi.vm.yti.codelist.intake.groupmanagement;

public class GroupManagementUser {

    private final String email;
    private final String firstName;
    private final String lastName;

    // Jackson constructor
    @SuppressWarnings("unused")
    private GroupManagementUser() {
        this("", "", "");
    }

    public GroupManagementUser(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
