/**
 * 
 */
package io.smartspaces.messaging.route.ros;

import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;

import java.nio.charset.Charset;

import com.google.common.base.Charsets;

/**
 * @author keith
 *
 */
public class MapGenericMessageCodec {

  /**
   * The JSON mapper for message translation.
   */
  protected static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The character set for the generic message encoding.
   */
  protected Charset charset = Charsets.UTF_8;
}
