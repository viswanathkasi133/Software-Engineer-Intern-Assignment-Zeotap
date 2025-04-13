/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.wrangler.api;

import io.cdap.wrangler.api.annotations.Public;

/**
 * A triplet consisting of three elements - first, second & third.
 *
 * This class provides immutable access to elements of the triplet.
 *
 * @param <F> type of the first element
 * @param <S> type of the second element
 * @param <T> type of the third element
 */
@Public
public final class Triplet<F, S, T> {
  private final F first;
  private final S second;
  private final T third;

  public Triplet(F first, S second, T third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  /**
   * @return First element of the triplet.
   */
  public F getFirst() {
    return first;
  }

  /**
   * @return Second element of the triplet.
   */
  public S getSecond() {
    return second;
  }

  /**
   * @return Third element of the triplet.
   */
  public T getThird() {
    return third;
  }
}
