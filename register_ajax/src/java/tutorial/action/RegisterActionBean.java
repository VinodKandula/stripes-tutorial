/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tutorial.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.DontBind;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HttpCache;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.validation.BooleanTypeConverter;
import net.sourceforge.stripes.validation.DateTypeConverter;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 *
 * @author hantsy
 */
public class RegisterActionBean extends BaseActionBean {

    @Validate(required = true, mask = "[0-9a-zA-Z]{6,20}")
    private String username;
    @Validate(required = true, minlength = 6, maxlength = 20, mask = "[0-9a-zA-Z]+")
    private String password;
    @Validate(required = true, converter = EmailTypeConverter.class)
    private String email;
    @Validate(required = true, expression = "this eq password")
    private String confirmPassword;
    @Validate(converter = DateTypeConverter.class)
    Date birthDate;
    @Validate(converter = BooleanTypeConverter.class)
    boolean subscriptionEnabled;
    @ValidateNestedProperties({
        @Validate(field = "zipcode", required = true),
        @Validate(field = "addressLine1", required = true),
        @Validate(field = "addressLine2", required = true)
    })
    private Address address;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public boolean isSubscriptionEnabled() {
        return subscriptionEnabled;
    }

    public void setSubscriptionEnabled(boolean subscriptionEnabled) {
        this.subscriptionEnabled = subscriptionEnabled;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Resolution register() {
        addMessage("Registered successfully!");
        addMessage("Congratulations!");
        return new RedirectResolution("/success.jsp").flash(this);
    }

    public void addMessage(String message) {
        FlashScope scope = FlashScope.getCurrent(getContext().getRequest(), true);
        List<String> messages = (List<String>) scope.get("messages");
        if (messages == null) {
            messages = new ArrayList<String>();
            scope.put("messages", messages);
        }
        messages.add(message);
    }

    @ValidationMethod(on = "register")
    public void userExsited(ValidationErrors errors) {
        if ("testuser".equals(username)) {
            errors.add("username", new SimpleError("This username is taken , please select a different one."));
        }
        if (!errors.isEmpty()) {
            errors.addGlobalError(new SimpleError("Error ocurs on save. Please fix it firstly."));
        }
    }
    private String captchaResponse;

    public String getCaptchaResponse() {
        return captchaResponse;
    }

    public void setCaptchaResponse(String captchaResponse) {
        this.captchaResponse = captchaResponse;
    }

    @ValidationMethod(on = "register")
    public void verifyCode(ValidationErrors errors) {
        String captchaId = getContext().getRequest().getSession().getId();
        boolean isResponseCorrect = CaptchaManager.get().validateResponseForID(captchaId, captchaResponse);
        if (!isResponseCorrect) {
            errors.add("register.captcha.error",
                    new SimpleError("Captcha code missmatch!"));
        }

    }

    @DontValidate
    @HttpCache(allow = false)
    public Resolution checkUser() {
        System.out.println("@@@checkUser method invoked...@@@");
        System.out.println("@@@ username value is:" + username);
        return new StreamingResolution("text/plain") {

            @Override
            protected void stream(HttpServletResponse response) throws Exception {
                if ("testuser".equals(username)) {
                    response.getWriter().write("true");
                } else {
                    response.getWriter().write("false");
                }
            }
        };
    }

    @DontValidate
    @DontBind
    @HttpCache(allow = false)
    public Resolution refresh() {
        return new ForwardResolution("/captcha.jsp");
    }

}
