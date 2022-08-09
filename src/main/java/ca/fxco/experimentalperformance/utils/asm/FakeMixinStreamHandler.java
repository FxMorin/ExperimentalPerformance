package ca.fxco.experimentalperformance.utils.asm;

import ca.fxco.experimentalperformance.ExperimentalPerformance;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.Permission;
import java.util.Map;
import java.util.function.BiConsumer;

import static ca.fxco.experimentalperformance.utils.GeneralUtils.formatPathDot;

public final class FakeMixinStreamHandler extends URLStreamHandler {

    public static BiConsumer<String, byte[]> sign;
    private final Map<String, byte[]> mixins;

    public static URL createURL(Map<String, byte[]> mixins) throws MalformedURLException {
        return new URL("magic-at", null, -1, "/", new FakeMixinStreamHandler(mixins));
    }

    public FakeMixinStreamHandler(Map<String, byte[]> mixins) {
        this.mixins = mixins;
    }

    @Override
    protected URLConnection openConnection(URL url) {
        return mixins.containsKey(url.getPath()) ? new FakeMixinConnection(url, mixins.get(url.getPath())) : null;
    }

    private static final class FakeMixinConnection extends URLConnection {
        private final byte[] stream;

        public FakeMixinConnection(URL url, byte[] stream) {
            super(url);
            this.stream = stream;
        }

        @Override
        public InputStream getInputStream() {
            String path = url.getPath();
            if (sign != null) {
                sign.accept(formatPathDot(path.substring(1, path.length() - 6)), stream);
            } else if (ExperimentalPerformance.VERBOSE) {
                ExperimentalPerformance.LOGGER.warn("FakeMixinConnection called while `sign` has not been set yet!");
            }
            return new ByteArrayInputStream(stream);
        }

        @Override
        public void connect() {} // Do nothing

        @Override
        public Permission getPermission() {
            return null;
        }
    }
}
