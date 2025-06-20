/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.future0923.debug.tools.test.simple.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

/**
 * @author future0923
 */
public class GsonTest {

    private static final Gson gson = new Gson();

    public static class DTO {

        private String name;

        private Integer age;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Test {
        public static String test() {
            DTO dto = new DTO();
            dto.setName("Gson");
            dto.setAge(18);
            String jsonStr = gson.toJson(dto);
            System.out.println(jsonStr);
            return jsonStr;
        }

        public static String node() throws JsonProcessingException {
            List<Node> list = Arrays.<Node>asList(new Node(new TestNode("testNode1", 1, null), "node1"), new Node(new TestNode("testNode2", 2,null), "node2"));
            TestNode testNode = new TestNode("testNode", 0, list);
            String jsonStr = gson.toJson(testNode);
            System.out.println(jsonStr);
            gson.fromJson(jsonStr, TestNode.class);
            return jsonStr;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(1000000000L);
    }
}
