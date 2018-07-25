package com.demo.microservice.web;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.xxyy.log.LogUtil;
import com.xxyy.result.JsonResult;
import com.xxyy.result.JsonResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 测试Eureka接口
 * @author dangzhizheng
 */
@Controller
@RequestMapping(value = "/api")
public class EurekaTest {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取eureka服务信息
     * @return
     */
    @RequestMapping(value = "/getEurekaServer", method = RequestMethod.GET)
    @ResponseBody()
    public JsonResult getEurekaServer() {
        try {
            LogUtil.info("getEurekaServerList");
            // 获取eureka server list的信息
            List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances("MICROSERVICE-PORVIDER");
            List<String> serviceList = discoveryClient.getServices();
            return JsonResultUtil.getJsonResult(JsonResultUtil.Code.SUCCESS, serviceList,
                    JsonResultUtil.Code.SUCCESS.message);
        } catch (Exception e) {
            LogUtil.error(getClass(), "操作出错", e);
            return JsonResultUtil.getJsonResult(JsonResultUtil.Code.ERROR, "操作出错");
        }
    }

    /**
     * 调用注册到eureka的服务
     * @return
     */
    @RequestMapping(value = "/consumerService", method = RequestMethod.GET)
    @ResponseBody()
    public JsonResult consumerService() {
        try {
            LogUtil.info("consumerService");
            // 通过restTemplate调用eureka的服务，restTemplate必须添加ribbon注解
            Object object = restTemplate.getForObject("http://microservice-provider/api/testApi", Map.class);
            return JsonResultUtil.getJsonResult(JsonResultUtil.Code.SUCCESS, object,
                    JsonResultUtil.Code.SUCCESS.message);
        } catch (Exception e) {
            LogUtil.error(getClass(), "操作出错", e);
            return JsonResultUtil.getJsonResult(JsonResultUtil.Code.ERROR, "操作出错");
        }
    }

    /**
     * 调用注册到eureka的服务，提供断路器功能
     * @return
     */
    // 断路器，指定fallback函数
    @HystrixCommand(fallbackMethod = "consumerServiceFallback")
    @RequestMapping(value = "/consumerServiceWithHystrix", method = RequestMethod.GET)
    @ResponseBody()
    public JsonResult consumerServiceWithHystrix() {
        try {
            LogUtil.info("consumerService with Hystrix");
            Object object = restTemplate.getForObject("http://microservice-provider/api/testApi", Map.class);
            return JsonResultUtil.getJsonResult(JsonResultUtil.Code.SUCCESS, object,
                    JsonResultUtil.Code.SUCCESS.message);
        } catch (Exception e) {
            LogUtil.error(getClass(), "操作出错", e);
            return JsonResultUtil.getJsonResult(JsonResultUtil.Code.ERROR, "操作出错");
        }
    }

    public JsonResult consumerServiceFallback() {
        try {
            LogUtil.info("consumerService with Hystrix fallback");
            String message = "调用eureka服务失败，使用回退方法";
            return JsonResultUtil.getJsonResult(JsonResultUtil.Code.SUCCESS, message,
                    JsonResultUtil.Code.SUCCESS.message);
        } catch (Exception e) {
            LogUtil.error(getClass(), "操作出错", e);
            return JsonResultUtil.getJsonResult(JsonResultUtil.Code.ERROR, "操作出错");
        }
    }




}
