/*
 * Interface and generalization relationships in Jakarta Catalina
 */

class HttpResponseBase
	extends ResponseBase
	implements HttpResponse, HttpServletResponse {}

abstract class HttpResponseWrapper
	extends ResponseWrapper 
	implements HttpResponse {}

class HttpResponseFacade
	extends ResponseFacade 
	implements HttpServletResponse {}

abstract class ResponseWrapper implements Response {}
abstract interface HttpResponse extends Response {}
abstract class ResponseBase implements Response, ServletResponse {}
abstract interface HttpServletResponse {}
class ResponseFacade implements ServletResponse {}

abstract interface ServletResponse {}
abstract interface Response {}
