/*
 * Copyright (C) 2018 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.smartspaces.service.web.server

/**
 * An HTTP filter chain.
 * 
 * @author Keith M. Hughes
 */
object HttpFilterChain {
 
  /**
   * Create a filtered GET handler.
   * 
   * @param filters
   *        the filters to use
   * @param finalHandler
   *        the GET handler
   * 
   * @return the handler
   */
  def newFilteredGetHandler(filters: List[HttpFilter], finalHandler: HttpGetRequestHandler): HttpGetRequestHandler = {
    new FilteredHttpDynamicGetRequestHandler(newGetFilterChain(filters, finalHandler))
  }
  
  /**
   * Create a filter chain for an GET handler.
   * 
   * @param filters
   *        the filters to use
   * @param finalHandler
   *        the GET handler
   * 
   * @return the formed chain
   */
  def newGetFilterChain(filters: List[HttpFilter], finalHandler: HttpGetRequestHandler): HttpFilterChain = {
    filters.foldRight(new HttpGetFilterChainEnd(finalHandler).asInstanceOf[HttpFilterChain]) ((filter, chain) => new HttpFilterChainIntermediate(filter, chain).asInstanceOf[HttpFilterChain])
  }
 
  /**
   * Create a filtered OPTIONS handler.
   * 
   * @param filters
   *        the filters to use
   * @param finalHandler
   *        the OPTIONS handler
   * 
   * @return the handler
   */
  def newFilteredOptionsHandler(filters: List[HttpFilter], finalHandler: HttpOptionsRequestHandler): HttpOptionsRequestHandler = {
    new FilteredHttpDynamicOptionsRequestHandler(newOptionsFilterChain(filters, finalHandler))
  }
  
  /**
   * Create a filter chain for an OPTIONS handler.
   * 
   * @param filters
   *        the filters to use
   * @param finalHandler
   *        the OPTIONS handler
   * 
   * @return the formed chain
   */
  def newOptionsFilterChain(filters: List[HttpFilter], finalHandler: HttpOptionsRequestHandler): HttpFilterChain = {
    filters.foldRight(new HttpOptionsFilterChainEnd(finalHandler).asInstanceOf[HttpFilterChain]) ((filter, chain) => new HttpFilterChainIntermediate(filter, chain).asInstanceOf[HttpFilterChain])
  }
}

/**
 * An HTTP filter chain.
 * 
 * @author Keith M. Hughes
 */
trait HttpFilterChain {
  
  /**
   * Do a filtering operation.
   * 
   * @param request
   *        the request
   * @param response
   *        the response
   */
  def doHttpFilter(request: HttpRequest, response: HttpResponse): Unit
}

/**
 * An intermediate element of a filter chain which calls a filter and includes the next part of the
 * chain.
 * 
 * @author Keith M. Hughes
 */
class HttpFilterChainIntermediate(filter:  HttpFilter, nextChainElement: HttpFilterChain) extends HttpFilterChain {
    override def doHttpFilter(request: HttpRequest, response: HttpResponse): Unit = {
   filter.doHttpFilter(request, response, nextChainElement)
  }

}

/**
 * The end of the filter chain for a GET HTTP handler.
 * 
 * @author Keith M. Hughes
 */
class HttpGetFilterChainEnd(finalHandler: HttpGetRequestHandler) extends HttpFilterChain {
  override def doHttpFilter(request: HttpRequest, response: HttpResponse): Unit = {
    finalHandler.handleGetHttpRequest(request, response)
  }
}

/**
 * The end of the filter chain for an OPTIONS HTTP handler.
 * 
 * @author Keith M. Hughes
 */
class HttpOptionsFilterChainEnd(finalHandler: HttpOptionsRequestHandler) extends HttpFilterChain {
  override def doHttpFilter(request: HttpRequest, response: HttpResponse): Unit = {
    finalHandler.handleOptionsHttpRequest(request, response)
  }
}

/**
 * An HTTP GET request handler that uses a filter.
 * 
 * @author Keith M. Hughes
 */
class FilteredHttpDynamicGetRequestHandler(filterChain: HttpFilterChain) extends HttpGetRequestHandler {
  override def handleGetHttpRequest(request: HttpRequest, response: HttpResponse): Unit = {
    filterChain.doHttpFilter(request, response)
  }
}

/**
 * An HTTP OPTIONS request handler that uses a filter.
 * 
 * @author Keith M. Hughes
 */
class FilteredHttpDynamicOptionsRequestHandler(filterChain: HttpFilterChain) extends HttpOptionsRequestHandler {
  override def handleOptionsHttpRequest(request: HttpRequest, response: HttpResponse): Unit = {
    filterChain.doHttpFilter(request, response)
  }
}

/**
 *A filter for HTTP requests.
 * 
 * @author Keith M. Hughes
 */
trait HttpFilter {
  
  /**
   * Perform the filtering operation.
   * 
   * @param request
   *        the HTTP request
   * @param response
   *        the HTTP response
   * @param filterChain
   *        the filter chain
   */
  def doHttpFilter(request: HttpRequest, response: HttpResponse, filterChain: HttpFilterChain): Unit
}
