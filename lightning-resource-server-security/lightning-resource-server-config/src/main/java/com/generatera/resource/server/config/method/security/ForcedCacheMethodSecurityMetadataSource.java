package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.resource.server.config.method.security.entity.ResourceMethodSecurityEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.AbstractMethodSecurityMetadataSource;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author FLJ
 * @date 2023/2/10
 * @time 9:48
 * @Description 强制性的缓存方法 元数据 source ..
 * <p>
 * 使用延迟队列进行 强制更新 ...
 *
 * 也就是支持更新 ..
 */
public class ForcedCacheMethodSecurityMetadataSource extends AbstractMethodSecurityMetadataSource {


    private final LightningExtMethodSecurityMetadataSource delegate;
    private final boolean supportForceUpdate;
    private final long updateDuration;

    private final static MetadataSourceAllRefreshEvent event = new MetadataSourceAllRefreshEvent(new Object());

    /**
     * 额外的任务 ...
     */
    private final DelayQueue<MetadataWithDelayed> delayQueue = new DelayQueue<>();

    private final Object monitor = new Object();

    /**
     * 自动处理 ...(当没有线程活动时) ..
     * <p>
     * // TODO: 2023/2/10  基于spring bean 生命周期来做这个事情 ...
     */
    private final Timer timer = new Timer(true);

    /**
     * 上一次实际执行时间
     */
    private long lastExecuteAt;

    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            // 执行当前已经执行 ...
            lastExecuteAt = Instant.now().toEpochMilli();
            ApplicationEvent currentEvent = null;
            List<MetadataWithDelayed> needInvokes = new LinkedList<>();
            while (true) {
                MetadataWithDelayed poll = delayQueue.poll();
                if (poll != null) {
                    needInvokes.add(poll);
                    continue;
                }
                break;
            }
            // 存在时 ..
            if (needInvokes.size() > 0) {
                // 如果只有一个 ...
                if (needInvokes.size() == 1) {
                    MetadataWithDelayed next = needInvokes.iterator().next();
                    currentEvent = next.event != null ? next.event : event;
                    doOnApplicationEvent(currentEvent);
                } else {
                    // 否则如果是全局刷新的事件,直接过滤掉 ...
                    // 部分刷新
                    Collection<ResourceMethodSecurityEntity> applicationEvents = needInvokes.stream()
                            .map(MetadataWithDelayed::getEvent).filter(ele -> ele instanceof MetadataSourceRefreshEvent)
                            .map(ele -> ((MetadataSourceRefreshEvent) ele).getMethodSecurityInfo())
                            .flatMap(ele -> Stream.of(ele.toArray(ResourceMethodSecurityEntity[]::new)))
                            .toList();
                    //合成一个事件
                    currentEvent = new MetadataSourceRefreshEvent(applicationEvents);
                    doOnApplicationEvent(currentEvent);
                }
            }

            // 为空,添加一个到队列,保证循环 ..
            if (delayQueue.size() == 0) {
                synchronized (monitor) {
                    if (delayQueue.size() == 0) {
                        delayQueue.add(new MetadataWithDelayed(lastExecuteAt + updateDuration, event));
                    }
                }
            }

            if (currentEvent != null) {
                // 打印日志
                logger.info("schedule an method security metadata source flush success ,schedule event is " + currentEvent.getClass().getSimpleName() + "!!!");
            } else {
                logger.info("schedule an empty method security metadata source flush success !!!");
            }
        }
    };


    public ForcedCacheMethodSecurityMetadataSource(LightningExtMethodSecurityMetadataSource delegate,
                                                   boolean supportForceUpdate,
                                                   long updateDuration) {
        this.delegate = delegate;
        this.supportForceUpdate = supportForceUpdate;
        this.updateDuration = updateDuration;

        // 启动timer
        // 等待实际执行完毕的 任务调度时间 ...
        this.timer.schedule(timerTask, 0, updateDuration);
        // 第一次会立即执行
        this.lastExecuteAt = Instant.now().toEpochMilli();
    }

    public ForcedCacheMethodSecurityMetadataSource(LightningExtMethodSecurityMetadataSource delegate, long updateDuration) {
        this(delegate, false, updateDuration);
    }

    public ForcedCacheMethodSecurityMetadataSource(LightningExtMethodSecurityMetadataSource delegate) {
        this(delegate, false, ResourceServerProperties.AuthorityConfiguration.CacheConfig.DEFAULT_EXPIRED_DURATION);
    }

    private void doOnApplicationEvent(ApplicationEvent event) {
        delegate.onApplicationEvent(event);
    }

    public void onApplicationEvent(@NotNull ApplicationEvent event) {
        // 需要做特殊的事情 ...
        if (event instanceof MetadataSourceAllRefreshEvent || event instanceof MetadataSourceRefreshEvent) {
            // 如果支持
            if (supportForceUpdate) {
                synchronized (monitor) {
                    // 等价延时,因为已经部分更新 ...
                    delayQueue.clear();
                    // 重新增加一个任务 ...
                    delayQueue.add(new MetadataWithDelayed(lastExecuteAt + updateDuration, new MetadataSourceAllRefreshEvent(null)));
                }
                delegate.onApplicationEvent(event);
            } else {
                if (event instanceof MetadataSourceRefreshEvent refreshEvent) {
                    if (refreshEvent.isForceFlush()) {
                        delegate.onApplicationEvent(event);
                        // 直接处理了 ... (例如让刷新端点有机会直接强制刷新) ...
                        return ;
                    }
                }
                synchronized (monitor) {
                    // 不管任务有没有执行,都可以修改为部分更新 ..
                    // 但是有一个问题,如果timer 没有执行, 一直修改将导致  之前的指令失效 ..
                    // 所以如果已经没有执行,那么 需要强制执行之前的指令 ...
                    // 由于是延迟队列,直接往队列里增加指令 ..
                    delayQueue.add(new MetadataWithDelayed(lastExecuteAt + updateDuration, event));
                }
            }
        } else {
            // 其他事件放行 ..
            delegate.onApplicationEvent(event);
        }

    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        return delegate.getAttributes(method, targetClass);
    }


    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return delegate.getAllConfigAttributes();
    }


    private static class MetadataWithDelayed implements Delayed {

        private final long expiredAt;

        private final ApplicationEvent event;

        public MetadataWithDelayed(long expiredAt, ApplicationEvent event) {
            this.expiredAt = expiredAt;
            this.event = event;
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert(expiredAt, TimeUnit.MILLISECONDS) - unit.convert(Instant.now().toEpochMilli(), TimeUnit.MILLISECONDS);
        }

        public ApplicationEvent getEvent() {
            return event;
        }

        @Override
        public int compareTo(@NotNull Delayed o) {
            if (o instanceof MetadataWithDelayed value) {
                if (expiredAt == value.expiredAt) {
                    return 0;
                }
                return (int) (expiredAt - value.expiredAt);
            }
            // 总是在它之前 ..
            return -1;
        }
    }
}
