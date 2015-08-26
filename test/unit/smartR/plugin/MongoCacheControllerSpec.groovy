package smartR.plugin

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Test
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(MongoCacheController)
@TestMixin(GrailsUnitTestMixin)
class MongoCacheControllerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    @Test
    void testMongoConnection() {

        when:
        MongoCacheController.RetireveFromCache()

        then:
        response.text == 'hello'
    }
}
