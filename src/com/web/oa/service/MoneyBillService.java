package com.web.oa.service;

import com.web.oa.pojo.BaoxiaoBill;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface MoneyBillService {
    List<BaoxiaoBill> findLeaveBillListByUser(long id);

    List<Task> findTaskListByName(String username);

    //部署流程
    void deployProcess(String processName, InputStream in);

    List<Deployment> findDeploymentList();

    List<ProcessDefinition> findDefinitionList();

    //启动任务
    void saveBaoxiao(BaoxiaoBill baoxiaoBill);

    void saveStartProcess(Long id, String username);

    //查询当前的报销单信息
    BaoxiaoBill findBaoxiaoBillByTaskId(String taskId);

    //查询所有批注信息
    List<Comment> findCommentByTaskId(String taskId);

    //查询连线情况(之后要控制走向)
    List<String> findOutComeListByTaskId(String taskId);

    //提交任务
    void submitTask(Long id, String taskId, String comment, String username, String outcome);

    void deleteProcessDefinitionByDeploymentId(String deploymentId);

    BaoxiaoBill findBaoxiaoBillById(Long id);

    List<Comment> findCommentsByBillId(Long id);

    void deleteBaoXiaoBill(Long leaveBillId);

    Task findTaskByBussinessKey(String bussiness_key);

    //根据任务id获取流程定义对象
    ProcessDefinition findProcessDefinitionByTaskId(String taskId);

    //获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中
    Map<String, Object> findCoordingByTask(String taskId);

    //获取流程定义图
    InputStream findImageInputStream(String deploymentId, String imageName);
}
