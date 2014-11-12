<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<script src="script/jquery-2.1.1.js" ></script>
<script type="text/javascript">
<jsp:useBean id="SquareAuthBean" scope="session" class="com.squarecash4glass.util.SquareAuthBean"/>

var squareAuthBean = {
	applicationId: '<%=SquareAuthBean.getApplicationId()%>',
	redirectURI: '<%=SquareAuthBean.getRedirectURI()%>',
	role: '<%=SquareAuthBean.getRole()%>',
	state: '<%=SquareAuthBean.getState()%>'
};

function callPost(path){
	var form = $('<form></form>');

    form.attr("method", "post");
    form.attr("action", path);

    $.each(squareAuthBean, function(key, value) {
        var field = $('<input></input>');

        field.attr("type", "hidden");
        field.attr("name", key);
        field.attr("value", value);

        form.append(field);
    });

    // The form needs to be a part of the document in
    // order for us to be able to submit it.
    $(document.body).append(form);
    form.submit();
}

</script>
</head>
<body onload="callPost('<%=SquareAuthBean.getAuthUrl()%>')">

</body>
</html>