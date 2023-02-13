package com.generatera.security.authorization.server.specification.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RsaKeyConversionUtils {
    private static ResourceKeyConverterAdapter<RSAPublicKey> x509 = new ResourceKeyConverterAdapter<>(RsaKeyConverters.x509());
    private static ResourceKeyConverterAdapter<RSAPrivateKey> pkcs8 = new ResourceKeyConverterAdapter<>(RsaKeyConverters.pkcs8());


    public static RSAPublicKey convertRsaPublicKey(String rsaPublicKey) {
        return x509.convert(rsaPublicKey);
    }

    public static RSAPrivateKey convertRsaPrivateKey(String rsaPrivateKey) {
        return pkcs8.convert(rsaPrivateKey);
    }

    static class ResourceKeyConverterAdapter<T extends Key> implements Converter<String, T> {
        private ResourceLoader resourceLoader = new DefaultResourceLoader();
        private final Converter<String, T> delegate;

        ResourceKeyConverterAdapter(Converter<InputStream, T> delegate) {
            this.delegate = this.pemInputStreamConverter().andThen(this.autoclose(delegate));
        }

        public T convert(String source) {
            return (T)this.delegate.convert(source);
        }

        void setResourceLoader(ResourceLoader resourceLoader) {
            Assert.notNull(resourceLoader, "resourceLoader cannot be null");
            this.resourceLoader = resourceLoader;
        }

        private Converter<String, InputStream> pemInputStreamConverter() {
            return (source) -> {
                return source.startsWith("-----") ? this.toInputStream(source) : this.toInputStream(this.resourceLoader.getResource(source));
            };
        }

        private InputStream toInputStream(String raw) {
            return new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
        }

        private InputStream toInputStream(Resource resource) {
            try {
                return resource.getInputStream();
            } catch (IOException var3) {
                throw new UncheckedIOException(var3);
            }
        }

        private <T> Converter<InputStream, T> autoclose(Converter<InputStream, T> inputStreamKeyConverter) {
            return (inputStream) -> {
                try {
                    InputStream is = inputStream;

                    T var3;
                    try {
                        var3 = inputStreamKeyConverter.convert(is);
                    } catch (Throwable var6) {
                        if (inputStream != null) {
                            try {
                                is.close();
                            } catch (Throwable var5) {
                                var6.addSuppressed(var5);
                            }
                        }

                        throw var6;
                    }

                    if (inputStream != null) {
                        inputStream.close();
                    }

                    return var3;
                } catch (IOException var7) {
                    throw new UncheckedIOException(var7);
                }
            };
        }
    }

}
