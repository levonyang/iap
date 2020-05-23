package com.haizhi.iap.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
* @description Swagger2的配置类
* @author LewisLouis
* @date 2018/8/10
*/
@Configuration
@EnableWebMvc
@EnableSwagger2
public class SwaggerConfig {

    /**
    * @description 配置swagger2的扫描包
    * @return
    * @author LewisLouis
    * @date 2018/8/13
    */
    @Bean
    public Docket api() {
         Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(this.apiInfo())
                .select()  //select()函数返回一个ApiSelectorBuilder实例用来控制哪些接口暴露给Swagger来展现，本例采用指定扫描的包路径来定义，Swagger会扫描该包下所有Controller定义的API，并产生文档内容（除了被@ApiIgnore指定的请求）。
                .apis(RequestHandlerSelectors.basePackage("com.haizhi.iap"))
                .paths(PathSelectors.any())
                .build()
                 //.securitySchemes(Arrays.asList(apiKey()));
                 //.securitySchemes(securitySchemes());
                 .globalOperationParameters(setHeaderToken());
        return docket;
    }

     /**
     * @description 用来创建该Api的基本信息（这些基本信息会展现在文档页面中）
     * @return ApiInfo API界面描述信息
     * @author LewisLouis
     * @date 2018/8/13
     */
     private ApiInfo apiInfo() {
         return new ApiInfoBuilder()
                 .title("企业知识图谱后台 API")
                 .description("") //描述
                 .termsOfServiceUrl("") //服务条款
                 .contact(new Contact("海致星图", "http://www.stargraph.cn/", "info@stargraph.cn"))
                 .version("1.0")
                 .build();
    }

    private List<Parameter> setHeaderToken() {
        ParameterBuilder tokenPar = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<>();
        tokenPar.name("Authorization").description("token, 部分接口(如登录)无需填写，输入格式:'bearer access_token值', 举例:'bearer eyJhbGciOiJIUz...CI6MTUzNT'").
                modelRef(new ModelRef("string")).parameterType("header").required(false)
                .defaultValue("bearer xxxxxxx").
                build();
        pars.add(tokenPar.build());
        return pars;
    }
}
