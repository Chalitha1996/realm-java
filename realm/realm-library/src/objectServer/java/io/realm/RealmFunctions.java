/*
 * Copyright 2020 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.util.List;

import io.realm.internal.Util;
import io.realm.internal.util.BsonConverter;

// CR: Async execution model. Google Play Task??

/**
 * A <i>Realm functions<i> manager to call MongoDB functions.
 */
// TODO Timeout is currently handled uniformly through OkHttpNetworkTransport configured through RealmAppConfig
public class RealmFunctions {

    /**
     * Call a MongoDB Realm function synchronously.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function. Primitive Java types and their boxed
     *             equivalents (bool/Boolean, int/Integer, long/Long and String) are automatically
     *             converted to their {@link BsonValue} equivalents, while
     *             {@link BsonValue}s are left as is.
     * @return Result of the Stitch function.
     *
     * @throws IllegalArgumentException if any of the arguments could not be converted to
     * {@link BsonValue}s
     * @throws ObjectServerError if the request failed in some way.
     * // FIXME Any other errors that we should expect
     */
    public BsonValue callFunction(String name, List<?> args) {
        List<BsonValue> bsonArgs = BsonConverter.to(args.toArray());
        BsonDocument document = new BsonDocument();
        // FIXME Ensure that this is the right contract with ObjectServer
        document.append("arguments", new BsonArray(bsonArgs));
        String resultString = invoke(name, document.toJson());

        BsonDocument resultDocument = BsonDocument.parse(resultString);
        // FIXME How to retrieve result and guard if no values, etc. ...needs final convention to
        //  lower  layers
        BsonValue result = resultDocument.values().iterator().next().asArray().get(0);
        return result;
    }

    /**
     * Call a MongoDB Realm function synchronously with typed result.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function. Java types like int/Integer, long/Long and
     *             String are automatically converted to the {@link BsonValue} equivalents, while
     *             {@link BsonValue}s are left as is.
     * @param clz  The type that the functions result should be converted to. If conversion is not
     *             possible a {@link IllegalArgumentException} is throwed.
     * @return Result of the Stitch function.
     *
     * @throws IllegalArgumentException if any of the arguments could not be converted to
     * {@link BsonValue}s or if the result could not be converted to the requested {@code clz}.
     * @throws ObjectServerError if the request failed in some way.
     * // FIXME Any other errors that we should expect
     */
    <T> T callFunction(String name, List<?> args, Class<T> clz) {
        BsonValue value = callFunction(name, args);
        return BsonConverter.from(clz, value);
    }

    /**
     * Call a Stitch function asynchronously.
     *
     * This is the asynchronous equivalent of {@link #callFunction(String, List)}.
     *
     * @param name Name of the MongoDB Realm function to call.
     * @param callback The callback to invoke on success
     * @param args Arguments to the MongoDB Realm function.
     * @return Result of the MongoDB Realm function.
     *
     * // FIXME How are object server errors propagated through the callback mechanism.
     * @throws IllegalStateException if not called on a looper thread.
     *
     * @see #callFunction(String, List)
     */
    // FIXME Eliminating varargs in favor. Seems more convenient to have same methods name for
    //  typed/untyped variant and allowing trailing SAM callback lambda for Kotlin which more or
    //  less resembled the Task.addCompleteListener from old Stitch API.
    // FIXME Evaluate original asynchronous Stitch API relying on Google Play Tasks. For now just
    //  use a RealmAsyncTask
    //  https://docs.mongodb.com/stitch-sdks/java/4/com/mongodb/stitch/android/core/services/StitchServiceClient.html
    //  <ResultT> Task<ResultT> callFunction​(String name, List<?> args, Long requestTimeout, Class<ResultT> resultClass, CodecRegistry codecRegistry);
    RealmAsyncTask callFunctionAsync(String name, List<?> args, RealmApp.Callback<BsonValue> callback) {
        Util.checkLooperThread("Asynchronous functions is only possible from looper threads.");
        return new RealmApp.Request<BsonValue>(RealmApp.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public BsonValue run() throws ObjectServerError {
                return callFunction(name, args);
            }
        }.start();
    }

    // FIXME Evaluate how type conversion exceptions in async call are acting
    <T> RealmAsyncTask callFunctionAsync(String name, List<?> args, Class<T> clz, RealmApp.Callback<T> callback) {
        return callFunctionAsync(
                name,
                args,
                result -> callback.onResult(RealmApp.Result.withResult(BsonConverter.from(clz, result.get())))
        );
    }

    // FIXME Basically just wrapping to allow mocking it through mockito
    private String invoke(String name, String args) {
        // Native calling scheme is actually synchronous
        // CR: Authentication? Guess we are in a user scope here!?
        // FIXME For now just return args directly until actual native call is in place
        return args;
   }

//    private static native String nativeCallFunction(String name, @Nullable String argsJson, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);

}
