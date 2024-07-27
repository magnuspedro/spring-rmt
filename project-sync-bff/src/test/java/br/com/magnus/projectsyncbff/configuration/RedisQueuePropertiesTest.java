package br.com.magnus.projectsyncbff.configuration;

import com.github.sonus21.rqueue.core.RqueueEndpointManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RedisQueuePropertiesTest {

    @Mock
    private RqueueEndpointManager rqueueEndpointManager;

    @Mock
    private QueueProperties queueProperties;

    private RedisQueueProperties redisQueueProperties;

    @BeforeEach
    void setUp() {
        this.redisQueueProperties = new RedisQueueProperties(rqueueEndpointManager, queueProperties);
        when(queueProperties.detectPattern()).thenReturn("testQueue");
    }

    @Test
    void getMeasurePattern_registersQueueIfNotRegistered() {
        var result = redisQueueProperties.getDetectionMethod();

        assertEquals("testQueue", result);
        verify(rqueueEndpointManager).registerQueue("testQueue");
    }

    @Test
    void getMeasurePattern_doesNotRegisterQueueIfAlreadyRegistered() throws IllegalAccessException {
        var field = ReflectionUtils.findFields(RedisQueueProperties.class, f -> f.getName().equals("isRegistered"), ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).getFirst();
        field.setAccessible(true);
        field.set(redisQueueProperties, true);

        String result = redisQueueProperties.getDetectionMethod();

        assertEquals("testQueue", result);
        verify(rqueueEndpointManager, never()).registerQueue("testQueue");
    }

    @Test
    void getMeasurePattern_registersQueueOnlyOnce() {
        when(rqueueEndpointManager.isQueueRegistered("testQueue")).thenReturn(false);

        redisQueueProperties.getDetectionMethod();
        redisQueueProperties.getDetectionMethod();

        verify(rqueueEndpointManager, times(1)).registerQueue("testQueue");
    }
}