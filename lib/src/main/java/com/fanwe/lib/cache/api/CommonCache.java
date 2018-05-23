/*
 * Copyright (C) 2017 zhengjun, fanwe (http://www.fanwe.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fanwe.lib.cache.api;

/**
 * 通用缓存接口
 */
public interface CommonCache<T>
{
    /**
     * 放入缓存对象
     *
     * @param key
     * @param value
     * @return
     */
    boolean put(String key, T value);

    /**
     * 获得缓存对象
     *
     * @param key
     * @return
     */
    T get(String key);

    /**
     * 返回key对应的缓存
     *
     * @param key
     * @param defaultValue 如果获取的缓存为null或者不存在，则返回这个值
     * @return
     */
    T get(String key, T defaultValue);
}
