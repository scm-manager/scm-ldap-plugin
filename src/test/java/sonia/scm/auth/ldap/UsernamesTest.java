/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */

package sonia.scm.auth.ldap;

import com.google.common.base.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link Usernames}.
 * 
 * @author Sebastian Sdorra
 */
public class UsernamesTest {
  
  /**
   * Test {@link Usernames#containsDomain(String)}.
   */
  @Test
  public void testContainsDomain() {
    assertFalse(Usernames.containsDomain(null));
    assertFalse(Usernames.containsDomain(""));
    assertFalse(Usernames.containsDomain("tricia"));
    assertFalse(Usernames.containsDomain("\\tricia"));
    assertTrue(Usernames.containsDomain("hitchhiker\\tricia"));
  }
  
  /**
   * Test {@link Usernames#withoutDomain(String)}.
   */
  @Test
  public void testWithoutDomain() {
    assertEquals("tricia", Usernames.withoutDomain("tricia"));
    assertEquals("tricia", Usernames.withoutDomain("hitchhiker\\tricia"));
  }

  /**
   * Tests {@link Usernames#withoutDomain(String)} without value.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWithoutDomainNullValue() {
    Usernames.withoutDomain(null);
  }
  
  /**
   * Tests {@link Usernames#withoutDomain(String)} with empty value.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWithoutDomainWithEmptyValue() {
    Usernames.withoutDomain("");
  }
  
  /**
   * Tests {@link Usernames#extractDomain(String)}.
   */
  @Test
  public void testExtractDomain() {
    assertFalse(Usernames.extractDomain("tricia").isPresent());
    assertFalse(Usernames.extractDomain("\\tricia").isPresent());
    
    Optional<String> domain = Usernames.extractDomain("hitchhiker\\tricia");
    assertTrue(domain.isPresent());
    assertEquals("hitchhiker", domain.get());
  }
  
    /**
   * Tests {@link Usernames#withoutDomain(String)} without value.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testExtractDomainNullValue() {
    Usernames.extractDomain(null);
  }
  
  /**
   * Tests {@link Usernames#withoutDomain(String)} with empty value.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testExtractDomainWithEmptyValue() {
    Usernames.extractDomain("");
  }

}