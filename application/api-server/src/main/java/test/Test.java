package test;

import java.util.Base64;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		String authKey = "client1:secret";
		authKey = Base64.getEncoder().encodeToString(authKey.getBytes());
		System.out.println("authKey::::"+authKey);
	}

}
