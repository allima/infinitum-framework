**Contents**


# Introduction #

A `TokenGenerator` is responsible for generating shared-secret tokens. Implementations of this interface can be injected into a SharedSecretAuthentication bean in order to provide it with a custom token-generation policy.

# Implementing `TokenGenerator` #

`TokenGenerator` consists of just one method called `generateToken()`, which returns a string representing the shared secret. Below is an example implementation of `TokenGenerator`, which generates an MD5 hash based on the current date and a provided user password. This strategy generates a token which is unique per day. A more sophisticated strategy might make use of a session or user GUID.

```
public class MyTokenGenerator implements TokenGenerator {

    private String mUserPassword;

    public MyTokenGenerator(String password) {
        mUserPassword = password;
    }

    @Override
    public String generateToken() {
        Calendar c = Calendar.getInstance();
	int day = c.get(Calendar.DAY_OF_MONTH);
	int month = c.get(Calendar.MONTH);
	int year = Integer.parseInt(Integer.toString(c.get(Calendar.YEAR)).substring(2));
	String token = (day * 10 + month * 100 + year * 1000) + mUserPassword;
        StringBuilder ret = new StringBuilder();
	try {
	    byte[] bytes = token.getBytes("UTF-8");
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    byte[] hash = md.digest(bytes);
	    for (int i = 0; i < hash.length; i++) {
	        if ((0xff & hash[i]) < 0x10)
	            ret.append("0" + Integer.toHexString((0xFF & hash[i])));
	        else
                    ret.append(Integer.toHexString(0xFF & hash[i]));
	    }
	} catch (UnsupportedEncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (NoSuchAlgorithmException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return ret.toString();
    }

}
```

# Injecting a `TokenGenerator` #

A `TokenGenerator` can be provided to a `SharedSecretAuthentication` using the `setTokenGenerator` method, but the preferred way is to inject it as a bean configured in InfinitumCfgXml.

```
<rest>
    <property name="host">http://localhost/mywebservice</property>
    <authentication ref="tokenAuthentication" enabled="true" />
</rest>

<beans>
    <bean id="tokenGenerator" src="com.example.rest.TokenGenerator" />
    <bean id="tokenAuthentication" src="com.clarionmedia.infinitum.rest.impl.SharedSecretAuthentication">
        <property name="mGenerator" ref="tokenGenerator" />
    </bean>
</beans>
```