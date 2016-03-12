/**
 * 
 */
package io.smartspaces.messaging.route;


/**
 * A codec for messages.
 * 
 * <p>
 * Instances are threadsafe and can encode and decode multiple messages
 * simultaneously.
 * 
 * @param <I>
 *          the type of internal messages
 * @param <O>
 *          the type of outgoing messages
 * 
 * @author Keith M. Hughes
 */
public interface MessageCodec<I, O> extends MessageEncoder<I, O>, MessageDecoder<I, O> {
}
