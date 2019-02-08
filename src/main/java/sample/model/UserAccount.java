package sample.model;

public class UserAccount {

	private String loginUser;
	private String passwordUser;

	public UserAccount(){}

	public UserAccount(String loginUser, String passwordUser) {
		this.loginUser = loginUser;
		this.passwordUser = passwordUser;
	}

	public String getLoginUser() {
		return loginUser;
	}

	public String getPasswordUser() {
		return passwordUser;
	}

}
