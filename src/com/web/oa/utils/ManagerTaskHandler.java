package com.web.oa.utils;


import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.Employee;
import com.web.oa.service.EmployeeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class ManagerTaskHandler implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        //spring容器
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();

        //获取当前对象
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        //获取当前对象
        EmployeeService employeeService = (EmployeeService) context.getBean("employeeServiceImpl");
        //根据上司id查询上司对象
        Employee manager = employeeService.findEmployeeManager(activeUser.getManagerId());
        //设置个人任务的办理人
        delegateTask.setAssignee(manager.getName());
    }
}
